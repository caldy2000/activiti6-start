package act6;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormData;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.form.DateFormType;
import org.activiti.engine.impl.form.LongFormType;
import org.activiti.engine.impl.form.StringFormType;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration
public class EngineStartTest {

	@Test
	public void test1CreateProcessEngine() throws Exception {
		ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();

	}

	@Test
	public void test2() throws Exception {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		String pName = processEngine.getName();
		String ver = ProcessEngine.VERSION;
		System.out.println("ProcessEngine [" + pName + "] Version: [" + ver + "]");
	}

	@Test
	public void test3ReadBPMNFile() throws Exception {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

		RepositoryService repositoryService = processEngine.getRepositoryService();
		Deployment deployment = repositoryService.createDeployment().addClasspathResource("onboarding.bpmn20.xml")
				.deploy();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).singleResult();
		System.out.println("Found process definition [" + processDefinition.getName() + "] with id ["
				+ processDefinition.getId() + "]");
	}

	@Test
	public void test4ExecTask() throws Exception {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		Deployment deployment = repositoryService.createDeployment().addClasspathResource("onboarding.bpmn20.xml")
				.deploy();
		ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
				.deploymentId(deployment.getId()).singleResult();

		RuntimeService runtimeService = processEngine.getRuntimeService();
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("onboarding");
		System.out.println("Onboarding process started with process instance id ["
				+ processInstance.getProcessInstanceId() + "] key [" + processInstance.getProcessDefinitionKey() + "]");

		TaskService taskService = processEngine.getTaskService();
		FormService formService = processEngine.getFormService();
		HistoryService historyService = processEngine.getHistoryService();

		Scanner scanner = new Scanner(System.in);
		while (processInstance != null && !processInstance.isEnded()) {
			List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("managers").list();
			System.out.println("Active outstanding tasks: [" + tasks.size() + "]");
			for (int i = 0; i < tasks.size(); i++) {
				Task task = tasks.get(i);
				System.out.println("Processing Task [" + task.getName() + "]");
				Map<String, Object> variables = new HashMap<String, Object>();
				FormData formData = formService.getTaskFormData(task.getId());
				for (FormProperty formProperty : formData.getFormProperties()) {
					if (StringFormType.class.isInstance(formProperty.getType())) {
						System.out.println(formProperty.getName() + "?");
						String value = scanner.nextLine();
						variables.put(formProperty.getId(), value);
					} else if (LongFormType.class.isInstance(formProperty.getType())) {
						System.out.println(formProperty.getName() + "? (Must be a whole number)");
						Long value = Long.valueOf(scanner.nextLine());
						variables.put(formProperty.getId(), value);
					} else if (DateFormType.class.isInstance(formProperty.getType())) {
						System.out.println(formProperty.getName() + "? (Must be a date m/d/yy)");
						DateFormat dateFormat = new SimpleDateFormat("m/d/yy");
						Date value = dateFormat.parse(scanner.nextLine());
						variables.put(formProperty.getId(), value);
					} else {
						System.out.println("<form type not supported>");
					}
				}
				taskService.complete(task.getId(), variables);

				HistoricActivityInstance endActivity = null;
				List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
						.processInstanceId(processInstance.getId()).finished().orderByHistoricActivityInstanceEndTime()
						.asc().list();
				for (HistoricActivityInstance activity : activities) {
					if (activity.getActivityType() == "startEvent") {
						System.out.println("BEGIN " + processDefinition.getName() + " ["
								+ processInstance.getProcessDefinitionKey() + "] " + activity.getStartTime());
					}
					if (activity.getActivityType() == "endEvent") {
						// Handle edge case where end step happens so fast that the end step
						// and previous step(s) are sorted the same. So, cache the end step
						// and display it last to represent the logical sequence.
						endActivity = activity;
					} else {
						System.out.println("-- " + activity.getActivityName() + " [" + activity.getActivityId() + "] "
								+ activity.getDurationInMillis() + " ms");
					}
				}
				if (endActivity != null) {
					System.out.println("-- " + endActivity.getActivityName() + " [" + endActivity.getActivityId() + "] "
							+ endActivity.getDurationInMillis() + " ms");
					System.out.println("COMPLETE " + processDefinition.getName() + " ["
							+ processInstance.getProcessDefinitionKey() + "] " + endActivity.getEndTime());
				}
			}
			// Re-query the process instance, making sure the latest state is available
			processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId())
					.singleResult();
		}
		scanner.close();
	}

	/**
	 * 配置数据库表后，UI设计器连接该数据库，并将所有设计内容存入表中， 该测试将从数据库查询，不依赖xml
	 * 
	 * 经测试，代码可以从数据库查询部署、过程定义。并执行过程任务、传递任务所需数据。
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStartProcess() throws Exception {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		TaskService taskService = processEngine.getTaskService();
		DeploymentQuery deploymentNameQuery = repositoryService.createDeploymentQuery().deploymentName("testApp");
		long count = deploymentNameQuery.count();
		Assert.assertTrue(count > 0);

		// 每一个部署deployment由若干个流程组成，
		Deployment testApp = deploymentNameQuery.orderByDeploymenTime().desc().list().get(0);

		// 从部署中查询他包含的流程信息
		ProcessDefinition testProcess = repositoryService.createProcessDefinitionQuery().deploymentId(testApp.getId())
				.processDefinitionName("testProcess").singleResult();
		Assert.assertNotNull(testProcess);

		// 创建流程实例
		ProcessInstance testProcessInstance = runtimeService.startProcessInstanceById(testProcess.getId());
		// 查询流程中有哪些任务
		List<Task> testProcessTasks = taskService.createTaskQuery().deploymentId(testApp.getId())
				.processDefinitionId(testProcess.getId()).list();

		Assert.assertTrue(testProcessTasks.size() > 0);

		
		FormService formService = processEngine.getFormService();
		// 获取任务的表单信息
		Task task = testProcessTasks.get(0);
		TaskFormData taskFormData = formService.getTaskFormData(task.getId());
		// 获取所有表单参数
		List<FormProperty> formProperties = taskFormData.getFormProperties();
		FormProperty formProperty = formProperties.get(0);

		Map<String, Object> variables = new HashMap<String, Object>();
		if (StringFormType.class.isInstance(formProperty.getType())) {
			variables.put(formProperty.getId(), "输入属性的值");
		}
		// 执行任务，传入任务所需参数
		taskService.complete(task.getId(), variables);

		testProcessInstance = runtimeService.createProcessInstanceQuery().processInstanceId(testProcessInstance.getId())
				.singleResult();
		if (Objects.nonNull(testProcessInstance)) {
			Assert.assertTrue(testProcessInstance.isEnded());
		}
	}

	
	@Test
	public void testVacationProcess() throws Exception {
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		RuntimeService runtimeService = processEngine.getRuntimeService();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		TaskService taskService = processEngine.getTaskService();
		FormService formService = processEngine.getFormService();
		
		// 初始检查部署与流程是否存在
		Deployment testApp = repositoryService.createDeploymentQuery().deploymentKey("testAppKey")
		.latest().singleResult();
		
		Assert.assertNotNull(testApp);
		
		ProcessDefinition vacationRequestProcess = repositoryService.createProcessDefinitionQuery()
				.deploymentId(testApp.getId())
				.processDefinitionKey("vacationRequest")
				.singleResult();
		Assert.assertNotNull(vacationRequestProcess);
		
		// 启动流程实例
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("employeeName", "Kermit");
		variables.put("numberOfDays", new Integer(4));
		variables.put("vacationMotivation", "I'm really tired!");
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("vacationRequest", variables);
		Assert.assertNotNull(processInstance);
//		runtimeService.deleteProcessInstance(processInstance.getId(), "测试流程实例启动后删除");
//		Assert.assertNull(processInstance = runtimeService.createProcessInstanceQuery().processDefinitionId(processInstance.getId()).singleResult());
		
		// Fetch all tasks for the management group
		List<Task> tasks = taskService.createTaskQuery().taskCandidateGroup("management").list();
		for (Task task : tasks) {
		  Log.info("Task available: " + task.getName());
		}
		
		// 执行任务1
//		tasks = taskService.createTaskQuery().processDefinitionId(vacationRequestProcess.getId()).list();
		
		Assert.assertNotNull(tasks);
		Assert.assertTrue(tasks.size() > 0);
		Task task = tasks.get(0);
		
		String description = task.getDescription();
		logger.info("任务描述:{}", description);
		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("vacationApproved", "false");
		taskVariables.put("managerMotivation", "We have a tight deadline!");
		taskService.complete(task.getId(), taskVariables);
	}
	
	static Logger Log = LoggerFactory.getLogger(EngineStartTest.class);
	static Logger logger = Log;
}
