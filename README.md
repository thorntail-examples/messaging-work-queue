# Messaging Shared Work Queue Mission for WildFly Swarm

## Build and Deploy the Application

#### With Source to Image Build (S2I)

Run the following commands to apply and execute the OpenShift
templates that will configure and deploy the applications.

```bash
find . | grep openshiftio | grep application | xargs -n 1 oc apply -f

oc new-app --template=wfswarm-messaging-shared-work-queue-worker -p SOURCE_REPOSITORY_URL=https://github.com/ssorj/wfswarm-messaging-shared-work-queue -p SOURCE_REPOSITORY_REF=master -p SOURCE_REPOSITORY_DIR=worker
```
