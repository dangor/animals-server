# Brian's Animal Server

## Web service
Hostname: bdang-animalia.elasticbeanstalk.com
Port: 80

### API

Write facts with POST /animals/facts:
```
curl -H "Content-Type: application/json" -X POST -d '{ "subject": "otter", "rel": "lives", "object": "river" }' http://bdang-animalia.elasticbeanstalk.com/animals/facts
```

Get facts with GET /animals/facts/{id}:
```
curl http://bdang-animalia.elasticbeanstalk.com/animals/facts/f6c18044-39ad-408e-8dec-dfba8f9609be
```

Delete facts with DELETE /animals/facts/{id}:
```
curl -X DELETE http://bdang-animalia.elasticbeanstalk.com/animals/facts/f6c18044-39ad-408e-8dec-dfba8f9609be
```

Query facts with GET /animals/which?s={subject}&r={rel}&o={object}:
```
curl -X GET "http://bdang-animalia.elasticbeanstalk.com/animals/which?s=animal&r=isa&o=mammal"
```

Count facts with GET /animals/how-many?s={subject}&r={rel}&o={object}:
```
curl -X GET "http://bdang-animalia.elasticbeanstalk.com/animals/how-many?s=animal&r=isa&o=mammal"
```

Delete all facts with DELETE /animals/facts/all
```
curl -X DELETE http://bdang-animalia.elasticbeanstalk.com/animals/facts/all
```

## Deploying Locally
Clone repository:
```
git clone https://github.com/dangor/animals-server.git
```

Start web server (uses port 8080 by default). Requires [Maven](https://maven.apache.org/index.html):
```
mvn tomcat7:run
```

In the same directory, you may train the server with initial data:
```
./training/train.sh ./training/animalia_data.csv localhost 8080
```

## Functional Tests
1. Get Postman app for Chrome:
https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en
2. Import Postman collection by opening this link in Chrome:
https://www.getpostman.com/collections/e765dbce1e75530cff6f
3. Update the host and port variables in the first test step "post fact a" -> "Pre-request script" (default is bdang-animalia.elasticbeanstalk.com:80)
4. Run Animalia Tests from the left menu in Postman (you may have click through multiple menus)

## Query Inheritance
Although it was not in the spec, I've added inheritance to fact query API.

For example, you do not need to train the server that fox is both an animal and a canid. Instead, you can train fox=canid and canid=animal and queries will work as expected.

By default, inheritance inference is made on both the subject and object of the fact query. E.g. since otter is a mammal, and coyotes eat otters, then s=animal&r=eats&o=mammal will include 'coyote', even though coyotes don't eat ALL mammals, just ANY.

Inheritance can be modified or turned off in code in src/main/java/com/bdang/storage/TinkerGraphAccessor.java