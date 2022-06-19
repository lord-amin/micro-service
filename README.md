curl -X POST \
  http://192.168.100.165:9030/authentication-service/v1/oauth/token \
  -H 'Authorization: Basic cG9ydGFsLWNsaWVudC1pZDpwb3J0YWwtY2xpZW50LXNlY3JldA==' \  
  -H 'cache-control: no-cache' \
  -H 'content-type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW' \
  -F grant_type=password \
  -F username=admin \
  -F password=1
  
  
  curl -X GET \
    http://192.168.100.165:9030/demo-service/v1/api/hello \
    -H 'Authorization: Bearer 30c0f1f0-e5b7-49d5-8a9c-d0db816c0c3d' \
    -H 'Content-Type: application/json' \    
    -H 'cache-control: no-cache'