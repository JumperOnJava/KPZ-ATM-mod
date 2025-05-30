
# ATM Mod

## Principles Followed in the Code

### 1. DRY (Don't Repeat Yourself)

Codebase mostly doesn't contain repeated code unless it is too complex to avoid
 - `MoneyInputState` is used for both withdrawal and depositing. All differences are either on server or in translation strings
 - `TransferReceiverInputState` extends `MoneyInputState` because it needs same number input UI but also needs new username input field

### 2. KISS (Keep It Simple, Stupid)

 - ServerThreadExecutor, ServerPacketQueue, SimpleRequestQueue have some similar functionality,
and it maybe was possible to have some shared code,
but I chose not to overcomplicate as it would not have benefited this project in any way
- in `RequestEndpointAdaper` there is some duplicate code in some request type handlers but this code may be changed independently of each other (for example if more arguments added for registration we can change cofr without affecting login code). Abstracting duplicated code in this case just increases complexity and may introduce bugs while having no benefits  

### 3. SOLID Principles

#### **S - Single Responsibility Principle (SRP)**

Every class has its distinct role. 
For example 
`DatabaseUtil` is only responsible for querying database, 
`SimpleRequestQueue` only responsible for matching requests to responses, 
`ServerThreadExecutor` is only responsible for running code on server thread 

#### **O - Open/Closed Principle (OCP)**

All classes either extend existing classes or extend functionality using composition
`TextInput` extends built in `TextFieldWidget` and only adds functionality keeping 
`TransferReceiverInputState` extends `MoneyInputState` by adding user input field without changing any code in base class
Modifications to base classes are done only for bug fixes

#### **L - Liskov Substitution Principle (LSP)**

All states of ATM screen have same interface and can replace each other without disrupting program
All interface implementations have full functionality

#### **I - Interface Segregation Principle (ISP)**



#### **D - Dependency Inversion Principle (DIP)**

Most of the classes use DIP where it makes sense
`DatabaseBankService` constructor accepts `DatabaseUtil` instance to access database
`DefaultEndpointProvider` accepts `DatabaseBankService` instance to access bank api and `ServerThreadExecutor` to run some code on server thread


### 4. YAGNI (You Ain't Gonna Need It)

`BankEndpoints` interface can be split to multiple interfaces for different subsystems in future for different subsystems 
(something like account subsystem/ atm sperations subsystem/ transfers subsystems etc.) but i chose not to do this because
at this moment there is no need to extend this system. 

No extra behavior or systems are added unless directly required.

### 5. Composition Over Inheritance

`AtmScreen` uses states instead of each state being subclass of `AtmScreen`

### 6. Program to Interfaces, Not Implementations

Almost all code works trough interfaces and implementation can be changed to change behaviour
All ATM screen states implement `AtmScreenState` interface
`BankEndpoints` interface defines what endpoints server has, `DefaultBankEndpoints` implements this interface using `BankService` interface implementation

### 7. Fail Fast Principle

Anywhere where there is possibilty of an error it is handled ASAP. 
For example when handling database queries if any error happens exception thrown and error is reported to client immediately.
Also, if some request requirement isn't met error is also sent as soon as possible

## Patterns

### Builder

`Button` class has its own `Builder` class to allow creation of new buttons using method chaining. 
This also allows creating multiple instances of similar objects by reusing builder and defining shared properties (like size) and specifying only unique per object properties (like position)

### Command

Class `Button` accepts `Action` interface implementation as one of arguments on creation. When button is clicked it executes action's only `execute()` method without knowing what it does.
- Note: in java it is possible to implement single method interface by using lambda expression, and this is used for most of the actions to simplify code 

### Observer 

`TextInput` impements `Notifier` interface which allows `Listener` implementations to subscribe for updates. 
In this case this used for listening for updates in text fields

### State

`AtmScreen` is responsible for displaying ATM user interface, while states are responsible for providing ui elements for different user actions
Each state implements `AtmScreenState` interface and provides different actions for user - login, balance operations or viewing operation history

### Composite

`ComponentContainer` interface implementations (`Component` extends `ComponentContainer`) specify all components that object contains - 
that allows to get all components that object contains. 
This used in `AtmScreen` to get all interactable/visual elements from current state and add them to renderer and interaction handlers

### Adapter

`ServerPacketQueue` accepts `RequestHandlerProvider` as an argument which provides `RequestTypeHandler`s to `ServerPacketQueue`. 
`BankEndpoints` interface defines bank endpoints with arguments and return values. `RequestEndpointAdaper` provides `ServerPacketQueue` with `RequestTypeHandler`s which internally call `BankEndpoints` implementation methods.
