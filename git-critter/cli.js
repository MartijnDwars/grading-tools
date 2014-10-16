#!/usr/bin/env node
var fs = require('fs')
    , argv = require('minimist')(process.argv.slice(2), { '--': true })
    , Q = require('q')  ;

var commands = fs.readdirSync(__dirname + '/commands')
                          .filter(function(file) { return /.js$/.test(file); })
                          .map(function(file) { return './commands/' + file; })
                          .map(require);

var commandName = argv._[0];

var command = null;
for (var index in commands) {
  if (commands[index].command == commandName) {
    command = commands[index];
    break;
  }
}

if (command == null) {
  for (var index in commands) {
    console.log("Command " + commands[index].command);
  }
} else {
  var options = {};
  for (var key in command.params) {
    var value = argv[key];
    if (value) {
      options[key] = value;
    } else if (command.params[key].required) {
      console.warn("Parameter '", key, "' is required.");
      process.exit(-1);
    }
  }
  options['--'] = argv['--'];
  
  var result = Q(command.execute(options));
  result.then(function(res) {
    if (res) {
      console.log(res);
    }
  }).catch(function(err) {
    console.warn("The command failed with the following error:");
    console.warn(err);
    console.warn(err.stack);
  }).done();
}