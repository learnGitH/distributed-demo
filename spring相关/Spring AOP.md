Spring AOP

一、代理

1.静态代理

示例：

接口：

```java
public interface Greeting {

    public void doGreet();

}
```

实现类：

```java
public class SayHello implements Greeting{
    @Override
    public void doGreet() {
        System.out.println("Greeting by say hello .");
    }
}

public class ShakeHands implements Greeting{
    @Override
    public void doGreet() {
        System.out.println("Greeting by shake other`s hands .");
    }
}

public class KissHello {

    public void doGreet(){
        System.out.println("Greeting by kiss . ");
    }

}
```

想在不改变代码的情况下，想在执行目标方法前后做一些操作，可以通过代理的方式来实现，下面利用静态代理来实现，静态代理需要创建代理类。代理类实现了和目标类一样的接口，代理类接收目标类对象，并在实现方法中调用目标类的实现方法前后做一些操作，如下：

```java
public class GreetStaticProxy implements Greeting{

    private Greeting hello;     //被代理对象
    public GreetStaticProxy(Greeting hello){
        this.hello = hello;
    }

    @Override
    public void doGreet() {
        before();   //执行其他操作
        this.hello.doGreet();
        after();    //执行其他操作
    }

    public void before(){
        System.out.println("[StaticProxy] Come to someone.");
    }
    public void after(){
        System.out.println("[StaticProxy] Back to his own corner");
    }

}
```

测试调用：

```java
public class Main {

    public static void main(String[] args){
        Greeting hello = new SayHello();
        Greeting shakeHands = new ShakeHands();

        //静态代理
        GreetStaticProxy staticHelloProxy = new GreetStaticProxy(hello);
        staticHelloProxy.doGreet();
        System.out.println();
        GreetStaticProxy shakeHandsProxy = new GreetStaticProxy(shakeHands);
        shakeHandsProxy.doGreet();
    }

}
```

运行结果：

```java
[StaticProxy] Come to someone.
Greeting by say hello .
[StaticProxy] Back to his own corner

[StaticProxy] Come to someone.
Greeting by shake other`s hands .
[StaticProxy] Back to his own corner
```

这个方式有弊端，如果有N个接口的实现类需要被代理，则需要创建N个代理类。

2.jdk动态代理

JDK动态代理主要用到java.lang.reflect包中的两个类：Proxy和InvocationHandler.

InvocationHandler是一个接口，通过实现该接口定义横切逻辑，并通过反射机制调用目标类代码，动态的将横切逻辑和业务逻辑编织在一起。

Proxy利用InvocationHandler动态创建一个符合某一接口的实例，生成目标类的代理对象。

示例如下:

接口：

```java
public interface Saying {

    public void sayHello(String name);
    public void talking(String name);

}
```

实现类：

```java
public class SayingImpl implements Saying{

    @Override
    public void sayHello(String name) {
        System.out.println(name + "：大家好啊！");
    }

    @Override
    public void talking(String name) {
        System.out.println(name + "：大家好啊！");
    }

}
```

现在我们要实现的是，在sayHello和talking之前和之后分别动态植入处理。如下，我们创建一个InvocationHandler实例:

```java
public class MyInvocationHandler implements InvocationHandler {

    private Object target;

    public MyInvocationHandler(Object target){
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //目标方法执行前
        System.out.println("——————————————————————————");
        System.out.println("下一位请登台发言！");
        //目标方法调用
        Object obj = method.invoke(target,args);
        //目标方法执行后
        System.out.println("大家掌声鼓励！");
        return obj;
    }
}
```

测试：

```java
public class JDKProxyTest {

    public static void main(String[] args){
        //希望被代理的目标业务类
        Saying target = new SayingImpl();
        //将目标类贺横切类编织在一起
        MyInvocationHandler handler = new MyInvocationHandler(target);
        //创建代理实例
        Saying proxy = (Saying) Proxy.newProxyInstance(
                target.getClass().getClassLoader(), //目标类的类加载器
                target.getClass().getInterfaces(),  //目标类的接口
                handler);                           //横切类
        proxy.sayHello("小明");
        proxy.talking("小丽");
    }

}
```

运行结果:

```java
——————————————————————————
下一位请登台发言！
小明：大家好啊！
大家掌声鼓励！
——————————————————————————
下一位请登台发言！
小丽：大家好啊！
大家掌声鼓励！
```

分析上面生成的代理类：

从Saying proxy = (Saying) Proxy.newProxyInstance(
                target.getClass().getClassLoader(), //目标类的类加载器
                target.getClass().getInterfaces(),  //目标类的接口
                handler);                           //横切类

产生以下的类说明，通过Proxy.newProxyInstance生成的代理类是依据传递进去的接口实现的，并且实现该接口的所有方法，并且在每个方法中调用传递进去handler的方法invoke

```java
public final class $Proxy0 extends Proxy implements Saying {
    private static Method m3;  //目标类的talking方法
    private static Method m4;	//目标类的sayHello方法

    public $Proxy0(InvocationHandler var1) throws  {
        super(var1);
    }

    public final void talking(String var1) throws  {
        try {
            //这里的super.h就是传递进去的MyInvocationHandler对象
            super.h.invoke(this, m3, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }

    public final void sayHello(String var1) throws  {
        try {
            super.h.invoke(this, m4, new Object[]{var1});
        } catch (RuntimeException | Error var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new UndeclaredThrowableException(var4);
        }
    }
}
```

<img src="https://img12.360buyimg.com/ddimg/jfs/t1/134044/32/22559/28177/618746d8Ed1cb73d7/70c217fbfec58a6a.png" alt="jdk动态代理.png" style="zoom: 80%;" />

使用JDK动态代理有一个很大的限制，就是它要求目标类必须实现了对应方法的接口，它只能为接口创建代理实例，我们在上文测试类中的Proxy的newProxyInstance方法中可以看到，该方法第二个参数便是目标类的接口。如果该类没有实现接口，这就要靠cglib动态技术了。

3.CGLib动态代理

CGLib采用非常底层的字节码技术，可以为一个类创建一个子类，并在子类中采用方法拦截的技术拦截所有父类方法的调用，并顺势植入横切逻辑。

导入包cglib-nodep-2.1_3.jar，如果使用spring的话就不用单独导入，因为spring默认已经引入了。

示例：

创建一个代理创建器CglibProxy:

```java
public class CglibProxy implements MethodInterceptor {

    Enhancer enhancer = new Enhancer();
    public Object getProxy(Class clazz){
        //设置需要创建的子类
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(this);
        //通过字节码技术动态创建子类实例
        return enhancer.create();
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println("——————————————————————————");
        System.out.println("下一位请登台发言！");
        //目标方法调用
        Object result = methodProxy.invokeSuper(o,objects);
        //目标方法后执行
        System.out.println("大家掌声鼓励！");
        return result;
    }

}
```

测试：

```java
public class CglibProxyTest {

    public static void main(String[] args){
        //将代理类存到本地磁盘
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "./");
        CglibProxy proxy = new CglibProxy();
        //通过动态生成子类的方式创建代理类
        Saying target = (Saying)proxy.getProxy(SayingImpl.class);
        target.sayHello("小明");
        target.talking("小丽");
    }

}
```

运行结果：

```java
CGLIB debugging enabled, writing to './'
——————————————————————————
下一位请登台发言！
小明：大家好啊！
大家掌声鼓励！
——————————————————————————
下一位请登台发言！
小丽：大家好啊！
大家掌声鼓励！
```

结果与JDK动态代理没有任何区别。CGLib动态代理能代理类和接口，但是不能代理final类，也是有一定局限性。JDK动态代理和CGLib动态代理都是运行时增强，通过将横切代码植入代理类的方式增强。与此不同的是AspectJ,它能够在通过特殊的编译器在编译时期将横切代码植入增强，这样的增强处理在运行时侯更有优势，因为JDK动态代理和CGLib动态代理每次运行都需要增强。

4.动态代理+责任链

有时我们对一个类可能有多种增强方式，例如前置通知增强，后置通知增强，异常通知增强，环绕通知增强，这些增强都需要嵌入目标方式中进行执行，此时就可以采用动态代理+责任链的方式来一一处理，spring aop就是采用该方式，下面看一个例子：

目标类：

```java
@Component
public class CalculateImpl implements Calculate{
    public int add(int numA, int numB) {
        System.out.println("执行目标方法:add");
        return numA+numB;
    }

    public int sub(int numA, int numB) {
        System.out.println("执行目标方法:reduce");
        return numA-numB;
    }

    public int div(int numA, int numB) {
        System.out.println("执行目标方法:div");
        return numA/numB;
    }

    public int multi(int numA, int numB) {
        System.out.println("执行目标方法:multi");
        return numA*numB;
    }

    public int mod(int numA,int numB){
        System.out.println("执行目标方法:mod");

        int retVal = ((Calculate) AopContext.currentProxy()).add(numA,numB);
        //int retVal = this.add(numA,numB);

        return retVal%numA;

        //return numA%numB;
    }
}
```

前置通知：

```java
public class LogAdvice implements MethodBeforeAdvice {

    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        String methodName = method.getName();
        System.out.println("执行目标方法【" + methodName + "】的<前置通知>,入参" + Arrays.asList(args));
    }
}
```

环绕通知：

```java
public class LogInterceptor implements MethodInterceptor {
    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        System.out.println(getClass()+"调用方法前");
        Object ret = methodInvocation.proceed();
        System.out.println(getClass()+"调用方法后");
        return ret;
    }
}
```

构建成链并执行测试：

```java
public class MainStart {

    public static void main(String[] args) throws Throwable {
        //把一条链上的都初始化
        List<MethodInterceptor> list = new ArrayList<>();
        list.add(new MethodBeforeAdviceInterceptor(new LogAdvice()));
        list.add(new LogInterceptor());

        //递归依次调用
        MyMethodInvocation invocation = new MyMethodInvocation(list);
        invocation.proceed();
    }

    public static class MyMethodInvocation implements MethodInvocation {

        protected List<MethodInterceptor> list;
        protected final CalculateImpl target;

        public MyMethodInvocation(List<MethodInterceptor> list){
            this.list = list;
            this.target = new CalculateImpl();
        }
        int i = 0;

        @Override
        public Method getMethod() {
            try{
                return target.getClass().getMethod("add",int.class,int.class);
            }catch (NoSuchMethodException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public Object[] getArguments() {
            return new Object[0];
        }

        @Override
        public Object proceed() throws Throwable {
            if (i == list.size()){
                return target.add(2,2);
            }
            MethodInterceptor mi = list.get(i);
            i++;
            return mi.invoke(this);
        }

        @Override
        public Object getThis() {
            return target;
        }

        @Override
        public AccessibleObject getStaticPart() {
            return null;
        }
    }

}
```

运行结构：

```
执行目标方法【add】的<前置通知>,入参[]
class com.haibin.springdemo.aop.earlyAopDemo.LogInterceptor调用方法前
执行目标方法:add
class com.haibin.springdemo.aop.earlyAopDemo.LogInterceptor调用方法后
```

