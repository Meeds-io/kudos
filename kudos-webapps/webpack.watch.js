const path = require('path');
const { merge } = require('webpack-merge');

const webpackProductionConfig = require('./webpack.prod.js');

module.exports = merge(webpackProductionConfig, {
  output: {
    path: '/exo-server/webapps/kudos/',
    filename: 'js/[name].bundle.js',
    libraryTarget: 'amd'
  },
  mode: 'development',
  devtool: 'eval-source-map'
});
