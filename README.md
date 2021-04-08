# Workflow

#### 介绍
建木流程引擎核心库

提供核心流程组件的定义与功能实现，实现了流程编排与流程流转的核心功能。

#### 核心概念

流程定义

流程实例

节点类型

异步任务实例


#### 节点类型

当前支持的节点类型如下：

开始节点（Start）

结束节点（End）

条件节点（Condition）

Switch节点（Switch）

For each节点（待实现）

异步任务节点（AsyncTask）

#### 参数类型支持

支持的基本类型如下：

string

number

boolean

datetime

file

password

url

email

支持的复合类型如下：

struct/object

array[any]

map<string, any>

#### 参数引用方式

参数值引用	`${workflow_ref.task_ref.parameter_ref}		`	

参数值为struct	`${workflow_ref.task_ref.parameter_ref.attribute_name}		`	

参数值为array或map	`${workflow_ref.task_ref.parameter_ref}[array_index or map_key]			`

