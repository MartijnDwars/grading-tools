var GitHubApi = require('github')
    , util = require('util');

/**
 * Temporary monkey-patch, solution in the pull-request:
 * https://github.com/mikedeboer/node-github/pull/194
 **/

var FixedAPI = module.exports = function(config) {
  GitHubApi.call(this, config);
  
  this.version = config.version; 
  var cls = require("github/api/v" + this.version); 
  var handler = this[this.version] = new cls(this); 
  
  var createParams = handler.routes.statuses.create.params;
  createParams.context = createParams.context || {
    "type": "String",
    "required": false,
    "validation": "",
    "invalidmsg": "",
    "description": "A string label to differentiate this status from the status of other systems."
  };
  
  var getCombined = handler.routes.statuses["get-combined"] = {
    "url": "/repos/:user/:repo/commits/:sha/status",
    "method": "GET",
    "params": {
      "$user": null,
      "$repo": null,
      "$sha": null
    }
  };
  
  handler.statuses.getCombined = function(msg, block, callback) {
    var self = this;
    this.client.httpSend(msg, block, function(err, res) {
        if (err)
            return self.sendError(err, null, msg, callback);

        var ret;
        try {
            ret = res.data && JSON.parse(res.data);
        }
        catch (ex) {
            if (callback)
                callback(new error.InternalServerError(ex.message), res);
            return;
        }

        if (!ret)
            ret = {};
        if (!ret.meta)
            ret.meta = {};
        ["x-ratelimit-limit", "x-ratelimit-remaining", "x-ratelimit-reset", "x-oauth-scopes", "link", "location", "last-modified", "etag", "status"].forEach(function(header) {
            if (res.headers[header])
                ret.meta[header] = res.headers[header];
        });

        if (callback)
            callback(null, ret);
    });
  };

  this.setupRoutes();

  return this;
};

util.inherits(FixedAPI, GitHubApi);