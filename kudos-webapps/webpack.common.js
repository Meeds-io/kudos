const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader')

const config = {
  context: path.resolve(__dirname, '.'),
  entry: {
    kudos: './src/main/webapp/vue-app/kudos/main.js',
    kudosAdmin: './src/main/webapp/vue-app/kudos-admin/main.js',
    kudosOverview: './src/main/webapp/vue-app/kudos-overview/main.js',
    engagementCenterExtensions: './src/main/webapp/vue-app/engagementCenterExtensions/extensions.js'
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        exclude: /node_modules/,
        use: [
          'babel-loader',
        ]
      },
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
        ]
      }
    ]
  },
  plugins: [
    new ESLintPlugin({
      files: [
        './src/main/webapp/vue-app/*.js',
        './src/main/webapp/vue-app/*.vue',
        './src/main/webapp/vue-app/**/*.js',
        './src/main/webapp/vue-app/**/*.vue',
      ],
    }),
    new VueLoaderPlugin()
  ],
  externals: {
    vuetify: 'Vuetify',
    vue: 'Vue',
    jquery: '$'
  }
};

module.exports = config;
