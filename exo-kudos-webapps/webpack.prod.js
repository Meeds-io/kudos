const path = require('path');
const merge = require('webpack-merge');
const webpackCommonConfig = require('./webpack.common.js');

const config = merge(webpackCommonConfig, {
  mode: 'production',
  module: {
    rules: [
      {
        test: /.(ttf|otf|eot|svg|woff(2)?)(\?[a-z0-9]+)?$/,
        use: {
          loader: "file-loader",
          options: {
            name: "/exo-kudos/fonts/[name].[ext]",
            emitFile: false
          }
        }
      }
    ]
  },
  entry: {
    kudos: './src/main/webapp/vue-app/kudos.js',
    kudosAdmin: './src/main/webapp/vue-app/kudosAdmin.js'
  },
  output: {
    path: path.join(__dirname, 'target/exo-kudos/'),
    filename: 'js/[name].bundle.js'
  },
  externals: {
    jquery: '$',
    vuetify: 'Vuetify'
  }
});

module.exports = config;
