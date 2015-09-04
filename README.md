This is a resolver for PACIFy which will ask the Automic ARA Webservice (service/DeploymentService.svc) for property values.

Here you find the parameters which the resolver is using:

| Parameter | Mandatary  | Default  | Description |
| ----------| ---------- | -------- | ----------- |
| araUrl  | yes  |  | What is host url of the ARA Webservice e.g. http://host:port . The service name (service/DeploymentService.svc) is hard coded, so you don't have to specify it. |
| username  | yes  |  | How to authenticate. e.g. AE4ARAI/2/OPPERMANS002/ARA |
| password  | yes  |  | The password token. |
| runId  | yes  |  | The runId of the running execution where you want to resolve the properties from. |
| target  | yes  |  | The target where you want to resolve the properties from. |
| component  | yes  |  | The component where you want to resolve the properties from. |
| namespace  | yes  |  | The namespace where you want to resolve the properties from. |
| decodePasswordWithBase64  | no  | false | ARA has problems with special characters in passwords. This is a workaround for that. Encode your password with base64, than save the result in ARA and set this property to true. |
| beginToken  | no  | @ | What is the begin token of a placeholder if you reference a property in a value. |
| endToken    | no  | @  | The end token of a placeholder. |
| propertyKeySeparator | no  | =>  | You can't use "." in property key's in ara, that why the property key is also saved in the value field. This is the separator which is used to separate the key from the value in the value column. E.g. log.level=>DEBUG |


[![PayPal donate button](https://www.paypalobjects.com/webstatic/en_US/btn/btn_donate_cc_147x47.png)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=5W7XC3MCM9XB2 "Donate to this project using Paypal")
