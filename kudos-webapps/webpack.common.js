const path = require('path');

const config = {
  context: path.resolve(__dirname, '.'),
  module: {
    rules: [
      {
        test: /\.vue$/,
        use: [
          'vue-loader',
          'eslint-loader',
        ]
      }
    ]
  },
  externals: {
    vuetify: 'Vuetify',
    vue: 'Vue',
    jquery: '$'
  }
};

module.exports = config;
