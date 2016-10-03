1.  install brew brew.sh  
2.  brew install sbt  
3.  brew install mysql
4.  mysql.server start
5.  brew install jenv
6.  run setup.sql
7.  edit /etc/hosts
    add 
    127.0.0.1	example.com
8.  sbt run
9. short url
   curl -X POST 'http://example.com:9000/shorten?url=http://google.com'
10. lengthen
   curl -X GET 'http://example.com:9000/lengthen?url=http://example:9000.com/Lm88Plk'

11. track click
    open browser and paste link created in step 9
12. clicks are tracked in log file logs/click_tracker.log delimited by |
