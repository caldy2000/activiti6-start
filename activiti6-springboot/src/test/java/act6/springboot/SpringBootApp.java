package act6.springboot;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.spring.boot.DataSourceProcessEngineAutoConfiguration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;


/*@SpringBootApplication(exclude = {
		DataSourceProcessEngineAutoConfiguration.class,
	    org.activiti.spring.boot.EndpointAutoConfiguration.class,
	    org.activiti.spring.boot.RestApiAutoConfiguration.class,
	    org.activiti.spring.boot.JpaProcessEngineAutoConfiguration.class,
	    org.activiti.spring.boot.SecurityAutoConfiguration.class,
	    DataSourceAutoConfiguration.class
})*/
@SpringBootApplication
@RestController
@RequestMapping("/api")
public class SpringBootApp {
	
	@RequestMapping(method = {RequestMethod.GET})
	@ResponseBody
	public String hello() {
		return "hello";
	}
	public static void main(String[] args) {
		ConfigurableApplicationContext app = SpringApplication.run(SpringBootApp.class, args);
		
	    
	}
	@Bean
    public CommandLineRunner init(final RepositoryService repositoryService,
                                  final RuntimeService runtimeService,
                                  final TaskService taskService) {

        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {
                System.out.println("Number of process definitions : "
                	+ repositoryService.createProcessDefinitionQuery().count());
                System.out.println("Number of tasks : " + taskService.createTaskQuery().count());
                runtimeService.startProcessInstanceByKey("vacationRequest");
                System.out.println("Number of tasks after process start: " + taskService.createTaskQuery().count());
            }
        };

    }
	
}
