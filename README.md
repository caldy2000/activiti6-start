# Activiti6
learning activiti6, based on mysql5.7, maven3. And Activiti UI is useful for designing BPMN diagrams.

Defult user/password for activiti-app: admin, test

and for activiti-admin: admin, admin

activiti-admin may need to change configuration to refer to ** app ** service, such as localhost:8080

# Environment config
follow official site guide, prepare db schema,and tomcat server to run web-ui apps.
# Record
## ProcessEngine
## Deployment
## ProcessDefinition
## Task

# Think
Application used to implement requirements only by code, like java web technologies,it is a way of hard code, make be enhanced by forms of configuration files.

When i first knew BPM(BPMN), i'm wondering why i need it, does it really accelerate business requirement implementation?

For the base idea of BPM, i think it is used to solve the cooperation of peoples in one organization.From start to the end of a workflow, it's visualized, and digital.It's much better than call each other.

Back to BPM implementation, Activiti is a wide used tool,and it can be used in Java Programs,developer can control workflow by Java programming language.


So,based on this idea, we could integrate any workflow to any other Application, it can across the organization maybe.
