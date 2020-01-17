# file-server
The Web File Server in Java based on Jetty

# Features
1. Proxy service download resources
2. Proxy configuration
3. Http/Https (SSL) configuration
4. Multiple root domains

# Requirements of Software
1. JRE 1.8

# How to use

For use the parameters bellow are supported. (All parameters should be configured by environment variables).

## Context Configuration
```
WELCOME_FILES - Welcome files (Default index.html). Ex: -DWELCOME_FILES="index.html,index.php"
FILE_PATH_SERVER - root path domain (Required only the WELCOME_FILES to be defined). Ex: -DFILE_PATH_SERVER="c:\\www\\htdocs\\"
DIRECTORIES_LISTED - 
FILES_PATH_SERVER - List of paths domain. 
                    The sintax is <ROOT PATH DOMAIN>;<DIRECTORIES LISTED - true OR false>;<WELCOME_FILE1.EXT,WELCOME_FILE2.EXT>|...
Ex: -DFILES_PATH_SERVER="c:\\www\\htdocs;true;index.html|c:\\www2\htdocs;false;index.php,index.html"
```

## Http Configuration
```
HTTP_PORT - Http Port (Default 8080)
HTTP_HOSTNAME - (Default localhost)
HTTP_PROTOCOL - (Default http/1.1)
```

## Https Configuration 
```
HTTPS_PORT - Https Port (Default 443)
HTTPS_HOSTNAME - Https Hostname (Default localhost)
SSL_ENABLED - SSL Configuration -> 0=Without SSL (Default), 1=Http and https enabled, 2=Just https enabled
KEYSTORE_FILE_PATH - Keystore path for SSL
KEYSTORE_PASS - Keystore password for SSL (Default empty)
```

## Web File Server as a Proxy Service
```
PROXY_SERVICE_CONFIG - Inform the context that will be intercepted, the URL that will be called and if the cache of the downloaded file should be kept. (JSON format). Ex: -DPROXY_SERVICE_CONFIG="[{\"context\":\"/repfiles\", \"url\":\"https://github.com/edubritochaves/file-server/repo/\", \"cache\":true}]"
```

## Proxy Configuration by access external URL
```
HTTP_PROXY - Http proxy configuration. Ex: http://proxyhost:8080@user:password
HTTP_PROXY_HOST - Http proxy's hostname (only used if HTTP_PROXY is not informed)
HTTP_PROXY_PORT - Http proxy's port (only used if HTTP_PROXY is not informed)
HTTP_PROXY_USER - Http proxy's user (only used if HTTP_PROXY is not informed)
HTTP_PROXY_PASSWORD - Http proxy's password (can be encoded if the parameter PASS_ENCODED is enabled)
HTTP_PROXY_TYPE - Http proxy's type (only SOCKS is supported)
HTTPS_PROXY - Https proxy configuration. Ex: http://proxyhost:8080@user:password
HTTPS_PROXY_HOST - Https proxy's hostname (only used if HTTP_PROXY is not informed)
HTTPS_PROXY_PORT - Https proxy's port (only used if HTTP_PROXY is not informed)
HTTPS_PROXY_USER - Https proxy's user (only used if HTTP_PROXY is not informed)
HTTPS_PROXY_PASSWORD - Https proxy's password (can be encoded if the parameter PASS_ENCODED is enabled)
HTTPS_PROXY_TYPE - Https proxy's type (only SOCKS is supported)
PASS_ENCODED - Password encoded configuration. (Default true).
```

## Demo

1. Runs the Java program with the following parameters:

2. Open the browser and put the URL bellow:
http://localhost:8080/vista-flags/128/Brazil-Flag-1-icon.png

3. After the download, load the image again.

![sample](https://github.com/edubritochaves/file-server/blob/master/repo/proxy-service-sample.png)

4. Look the Java console like as bellow:
```
starting File Server - 1.3
resource configuration => context: vista-flags, url: http://icons.iconarchive.com/icons/icons-land, cache: true
decoding password ...
has http proxy: false
has https proxy: false
http addres: localhost:8080
2020-01-16 23:30:57.917:INFO::main: Logging initialized @575ms to org.eclipse.jetty.util.log.StdErrLog
server is starting ...
welcome files: {index.html,index.php}
root path domain: c:\\www\\htdocs
2020-01-16 23:30:58.262:INFO:oejs.Server:Thread-0: jetty-9.4.11.v20180605; built: 2018-06-05T18:24:03.829Z; git: d5fc0523cfa96bfebfbda19606cad384d772f04c; jvm 1.8.0_211-b12
> 
2020-01-16 23:31:01.824:INFO:oejs.AbstractConnector:Thread-0: Started ServerConnector@7b2e044b{HTTP/1.1,[http/1.1]}{localhost:8080}
server started!
2020-01-16 23:31:01.824:INFO:oejs.Server:Thread-0: Started @4492ms
target: /vista-flags/128/Brazil-Flag-1-icon.png
downloading file: http://icons.iconarchive.com/icons/icons-land/vista-flags/128/Brazil-Flag-1-icon.png
getting input stream ...
http://icons.iconarchive.com/icons/icons-land/vista-flags/128/Brazil-Flag-1-icon.png downloaded! Saving file ...
file c:\\www\\htdocs\/vista-flags/128/Brazil-Flag-1-icon.png saved!
target: /vista-flags/128/Brazil-Flag-1-icon.png
found file /vista-flags/128/Brazil-Flag-1-icon.png in chache 
```

5. The image was cached in root domain.
