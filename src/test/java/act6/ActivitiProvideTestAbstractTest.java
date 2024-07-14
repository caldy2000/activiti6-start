package act6;

import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class ActivitiProvideTestAbstractTest {

	@Rule
	public ActivitiRule activitiRule = new ActivitiRule();
	
	/**
	 * Activiti对Junit4的支持，基于Rule自动扫描activiti配置文件创建引擎，
	 * {@link Deployment}注解实现了读取流程配置文件，自动创建流程并部署到数据库中，
	 * 单元测试结束会自动删除部署
	 * @throws Exception
	 */
	@Deployment(resources={
		"act6/ActivitiProvideTestAbstractTest.testGetEngine.bpmn20.xml"
	})
	@Test
	public void testGetEngine() throws Exception {
		RepositoryService repositoryService = activitiRule.getRepositoryService();
		RuntimeService runtimeService = activitiRule.getRuntimeService();
		ProcessDefinition process = repositoryService.createProcessDefinitionQuery()
		.processDefinitionId("vacationRequestUnitTest").singleResult();
		Assert.assertNotNull(process);
		
		TaskService taskService = activitiRule.getTaskService();
		List<Task> list = taskService.createTaskQuery().processDefinitionId("vacationRequestUnitTest").list();
		Assert.assertNotNull(list);
		Assert.assertTrue(list.size() >0);
	}
}
