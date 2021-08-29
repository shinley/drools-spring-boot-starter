# drools-spring-boot-starter


# 如何使用
1. 在pom中引入依赖
```
    <groupId>com.shinley.drools</groupId>
    <artifactId>drools-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
```
2. 在配置文件中配置 规则文件路径; 如果不配置， 默认使用 `rules/`
```
drools:
  rules:
    path: rules/
```
3. 编写规则文件

```
package test;
import com.shinely.drools.demo.model.Student
rule "test_01"
    when
        Student(age > 18)
    then
        System.out.println("你已达到法定年龄");
end
```

4. 在启动类中启用drools
 
```
    @EnableDrools
```

5. 在服务中触发规则

```

@Service
public class RuleService {
    @Autowired
    private KieBase kieBase;

    public void rule() {
        KieSession kieSession = kieBase.newKieSession();
        Student student = new Student();
        student.setAge(21);
        kieSession.insert(student);
        kieSession.fireAllRules();
        kieSession.dispose();;
    }
}

```


