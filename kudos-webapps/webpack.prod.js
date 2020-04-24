const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

const config = merge(webpackCommonConfig, {
  mode: 'production',
  entry: {
    kudos: './src/main/webapp/vue-app/kudos/main.js',
    kudosAdmin: './src/main/webapp/vue-app/kudos-admin/main.js',
    kudosOverview: './src/main/webapp/vue-app/kudos-overview/main.js'
  },
  output: {
    path: path.join(__dirname, 'target/kudos/'),
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  externals: {
    jquery: '$',
    vuetify: 'Vuetify'
  }
});

module.exports = config;
