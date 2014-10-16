var prompt = require('prompt')
    , Q = require('q')
    , GitHubApi = require('../github-fix.js');

var CloneUngradedCommand = module.exports = {
  command: "clone_ungraded",
  params: {
    "organisation": {
      required: true
    },
    "repository": {
      required: true
    }
  }
};

var username_password = [
  {
    name: 'username', 
    validator: /^[a-zA-Z_\-][a-zA-Z_\-0-9]*$/,
    warning: 'Username contains invalid characters'
  },
  {
    name: 'password',
    hidden: true
  }
];

(function() {
  this.array_map = Function.prototype.apply.bind(Array.prototype.map);
  this.array_flatten = function(array) {
    return array.reduce(function(a, b) {
      return a.concat(b);
    }, []);
  };

  this.promptCredentials = Q.nfbind(prompt.get, username_password);
  
  this.getReposFromOrg = function(github, org) {
    return Q.nbind(github.repos.getFromOrg, github.repos)({ org: org })
                .then(Q.nbind(this.appendNextPages, this, github));
  };
  
  this.getOpenPullRequestsFromRepo = function(github, repo) {
    return Q.nbind(github.pullRequests.getAll, github.pullRequests)({ user: repo.owner.login, repo: repo.name, state: 'open' })
                .then(Q.nfbind(this.appendNextPages, github));
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
        type: "basic",
        username: credentials.username,
        password: credentials.password
      });
      
      return self.getReposFromOrg(github, organisation)
        .then(self.filterReposByName.bind(undefined, repository))
        .then(function(repos) {
          return Q.all(repos.map(self.getOpenPullRequestsFromRepo.bind(self, github)));
        }).then(self.array_flatten)
        .then(function(pullRequests) {
          return Q.all(pullRequests.map(function(pr) {
            var getCombined = Q.nbind(github.statuses.getCombined, github.statuses);
            return getCombined({ user: pr.base.repo.owner.login, repo: pr.base.repo.name, sha: pr.head.sha }).then(function(state) {
              for(var index in state.statuses) {
                if (state.statuses[index].context == "continuous-integration/thatsgrade") {
                  return state.statuses[index];
                }
              }
              return null;
            }).then(function(state) {
              pr.status = state;
              return pr;
            });
          }));
        }).then(function(pullRequests) {
          return pullRequests.filter(function(pr) {
            return pr.status == null || pr.status.state == 'pending';
          });
        });
      });
  };
}).call(CloneUngradedCommand);