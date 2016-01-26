# Brian's Animal Server

## Deploying Locally
Clone repository:
```
git clone https://github.com/dangor/animals-server.git
```

Start web server:
```
mvn tomcat7:run
```

In the same directory, you may train the server with initial data:
```
./training/train.sh ./training/animalia_data.csv
```

## Functional Tests
1. Get Postman app for Chrome:
https://chrome.google.com/webstore/detail/postman/fhbjgbiflinjbdggehcddcbncdddomop?hl=en
2. Import Postman collection by opening this link in Chrome:
https://www.getpostman.com/collections/e765dbce1e75530cff6f
3. If you are not running against localhost:8080, you will have to update global config in the top right of Postman
4. Run Animalia Tests from the left menu in Postman (you may have click through multiple menus)

## Query Inheritance
Although it was not in the spec, I've added inheritance to fact query API.

For example, you do not need to train the server that fox is both an animal and a canid. Instead, you can train fox=canid and canid=animal and queries will work as expected.

By default, inheritance inference is made on both the subject and object of the fact query. E.g. since otter is a mammal, and coyotes eat otters, then s=animal&r=eats&o=mammal will include 'coyote', even though coyotes don't eat ALL mammals, just ANY.

Inheritance can be modified or turned off in code in src/main/java/com/bdang/storage/TinkerGraphAccessor.java