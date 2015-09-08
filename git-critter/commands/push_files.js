var prompt = require('prompt')
    , Q = require('q')
    , GitHubApi = require('../github-fix.js')
    , exec = require('child-process-promise').exec;

var PushFilesCommand = module.exports = {
  command: "push_files",
  params: {
    "organisation": {
      required: true
    },
    "repository": {
      required: true
    }
  }
};

var token = [{
  name: 'token'
}];

(function() {
  this.promptCredentials = Q.nfbind(prompt.get, token);
  
  this.getReposFromOrg = function(github, org) {
    return Q.nbind(github.repos.getFromOrg, github.repos)({ org: org })
                .then(Q.nbind(this.appendNextPages, this, github));
  };
  
  this.filterReposByName = function(regex, repos) {
    return repos.filter(function(repo) {
      return new RegExp(regex, "g").test(repo.name);
    });
  };
  
  this.appendNextPages = function(github, result, callback) {
    var self = this;
    if (github.hasNextPage(result)) {
      github.getNextPage(result, function(err, res) {
        if (err) {
          callback(err);
        } else {
          self.appendNextPages(github, res, function(err, rest) {
            if (err) {
              callback(err);
            } else {
              callback(null, result.concat(rest));
            }
          });
        }
      });
    } else {
      callback(null, result);
    }
  };

  /**
   * Connecting over https to a private repo requires you to authenticate. My
   * credentials were not accepted (perhaps because of 2FA). By connecting
   * over ssh, git uses your default pair.
   *
   * Just make sure the whoever executes this script has write access to the
   * student repos. E.g. by creating a "Staff" team, adding the TA to that
   * team, and adding the student repos with write access to that team.
   */
  this.httpsToSsh = function (cloneUrl) {
    return cloneUrl.replace('https://github.com/', 'git@github.com:');
  };
  
  this.execute = function(options) {
    var organisation = options.organisation;
    var repository = options.repository;
    
    var self = this;
    
    return self.promptCredentials().then(function(credentials) {
      var github = new GitHubApi({
        // required
        version: "3.0.0",
        // optional
        debug: false,
        protocol: "https",
        timeout: 5000
      });

      github.authenticate({
        type: "oauth",
        token: credentials.token
      });
      
      return self.getReposFromOrg(github, organisation)
        .then(self.filterReposByName.bind(undefined, repository))
        .then(function(repos) {
          return repos.reduce(function(sequence, repo) {
            return sequence.then(function(result) {
              var cloneUrl = self.httpsToSsh(repo.clone_url);

              return exec('git remote add ' + repo.name + ' ' + cloneUrl)
                        .catch(function(err) { console.warn(err.message); return null; }) // Ignore errors, may not be a good idea.
                        .then(function() { return result.concat([repo.name]); });
            });
          }, Q([]));
        }).then(function(remotes) {
          return remotes.reduce(function(sequence, remote) {
            return sequence.then(function(result) {
              return exec('git push ' + remote + ' ' + options['--'].join(' '))
                        .then(function(res) { console.log(remote); return res; })
                        .catch(function(err) { console.warn(err.message); return null; }) // Ignore errors, may not be a good idea.
                        .then(function() { return result.concat([remote]); });
            });
          }, Q([]));
        });
    });
  };
}).call(PushFilesCommand);