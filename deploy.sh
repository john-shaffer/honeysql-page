set -e

npx shadow-cljs release main
aws s3 cp public/honeysql-page/js/main.js s3://john-shaffer-com/honeysql-page/js/main.js
aws s3 sync public/honeysql-page/css s3://john-shaffer-com/honeysql-page/css
aws s3 cp public/index.html s3://john-shaffer-com/honeysql/index.html
aws cloudfront create-invalidation --distribution-id EAB0WURMU6NJY --paths "/honeysql-page/*" "/honeysql/*"
