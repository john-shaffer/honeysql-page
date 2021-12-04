#!/usr/bin/env bash

set -euxo pipefail

./build.clj release
aws s3 sync public/honeysql-page/css s3://john-shaffer-com/honeysql-page/css
aws s3 cp public/js/main.js s3://john-shaffer-com/honeysql/js/main.js
aws s3 sync public s3://john-shaffer-com/honeysql --exclude "honeysql-page/*" --exclude "js/*"
aws cloudfront create-invalidation --distribution-id EYL7DGPWAHVKY --paths "/honeysql-page/*" "/honeysql/*"
