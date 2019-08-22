const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

const config = merge(webpackCommonConfig, {
  mode: 'production',
  entry: {
    kudos: './src/main/webapp/vue-app/kudos.js',
    kudosAdmin: './src/main/webapp/vue-app/kudosAdmin.js'
  },
  output: {
    path: path.join(__dirname, 'target/kudos/'),
    filename: 'js/[name].bundle.js'
  },
  externals: {
    jquery: '$',
    vuetify: 'Vuetify'
  }
});

module.exports = config;
