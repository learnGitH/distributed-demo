package com.haibin.boot.future;

import java.util.concurrent.*;

public class CompletableFutureTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        test11();
    }

    //OR组合关系
    public static void test11(){
        //第一个异步任务，休眠2秒，保证它执行晚点
        CompletableFuture<String> first = CompletableFuture.supplyAsync(()->{
            try{

                Thread.sleep(2000L);
                System.out.println("执行完第一个异步任务");}
            catch (Exception e){
                return "第一个任务异常";
            }
            return "第一个异步任务";
        });

        ExecutorService executor = Executors.newSingleThreadExecutor();
        CompletableFuture<Void> future = CompletableFuture
                .supplyAsync(() -> {
                    System.out.println("执行完第二个任务");
                    return "第二个任务";
                },executor)
                //第三个任务
                .acceptEitherAsync(first,System.out::println,executor);
        executor.shutdown();
    }


    public static void test10(){
        CompletableFuture<String> first = CompletableFuture.completedFuture("第一个异步任务");
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "第二个异步任务",executor)
                .thenCombineAsync(first,(s,w) -> {
                    System.out.println(w);
                    System.out.println(s);
                    return "两个异步任务的组合";
                },executor);
        System.out.println(future.join());
        executor.shutdown();
    }

    public static void test1() throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        UserInfoService userInfoService = new UserInfoService();
        MedalService medalService = new MedalService();
        long userId =666L;
        long startTime = System.currentTimeMillis();

        //调用用户服务获取用户基本信息
        FutureTask<UserInfo> userInfoFutureTask = new FutureTask<>(new Callable<UserInfo>() {
            @Override
            public UserInfo call() throws Exception {
                return userInfoService.getUserInfo(userId);
            }
        });
        executorService.submit(userInfoFutureTask);

        Thread.sleep(300); //模拟主线程其它操作耗时

        FutureTask<MedalInfo> medalInfoFutureTask = new FutureTask<>(new Callable<MedalInfo>() {
            @Override
            public MedalInfo call() throws Exception {
                return medalService.getMedalInfo(userId);
            }
        });
        executorService.submit(medalInfoFutureTask);

        UserInfo userInfo = userInfoFutureTask.get();//获取个人信息结果
        MedalInfo medalInfo = medalInfoFutureTask.get();//获取勋章信息结果

        System.out.println("总共用时" + (System.currentTimeMillis() - startTime) + "ms");
    }

    public static void test2() throws InterruptedException, ExecutionException, TimeoutException {
        UserInfoService userInfoService = new UserInfoService();
        MedalService medalService = new MedalService();
        long userId =666L;
        long startTime = System.currentTimeMillis();

        //调用用户服务获取用户基本信息
        CompletableFuture<UserInfo> completableUserInfoFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return userInfoService.getUserInfo(userId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread.sleep(300); //模拟主线程其它操作耗时

        CompletableFuture<MedalInfo> completableMedalInfoFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return medalService.getMedalInfo(userId);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        UserInfo userInfo = completableUserInfoFuture.get(2,TimeUnit.SECONDS);//获取个人信息结果
        MedalInfo medalInfo = completableMedalInfoFuture.get();//获取勋章信息结果
        System.out.println("总共用时" + (System.currentTimeMillis() - startTime) + "ms");

    }

    public static void test3() {
        ExecutorService executor = Executors.newCachedThreadPool();
        //runAsync的使用
        CompletableFuture<Void> runFuture = CompletableFuture.runAsync(() -> System.out.println("test"),executor);
        //suppalyAsync的使用
        CompletableFuture<String> supplyFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("test2");
            return "test3";
        },executor);
        //runAsync的future没有返回值，输出null
        System.out.println(runFuture.join());
        //supplyAsync的future，有返回值
        System.out.println(supplyFuture.join());
        executor.shutdown(); // 线程池需要关闭
    }

    public static void test4() throws ExecutionException, InterruptedException {
        CompletableFuture<String> orgFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("test1");
            return "end1";
        });

        CompletableFuture thenRunFuture = orgFuture.thenRun(() -> {
            System.out.println("test2");
        });
        System.out.println(thenRunFuture.get());
    }

    public static void test5() throws ExecutionException, InterruptedException {
        CompletableFuture<String> orgFuture = CompletableFuture.supplyAsync(
                () -> {
                    System.out.println("test1");
                    return "end1";
                }
        );
        CompletableFuture thenAcceptFuture = orgFuture.thenAccept((a) -> {
            if ("end1".equals(a)) {
                System.out.println("关注了");
            }
            System.out.println("先考虑考虑");
        });
        System.out.println(thenAcceptFuture.get());
    }

    public static void test6() throws ExecutionException, InterruptedException {
        CompletableFuture<String> orgFuture = CompletableFuture.supplyAsync(
                () -> {
                    System.out.println("test1");
                    return "end1";
                }
        );

        CompletableFuture<String> thenApplyFuture = orgFuture.thenApply((a) -> {
            if ("end1".equals(a)) {
                System.out.println("关注了");
            }
            return "先考虑考虑";
        });
        System.out.println(thenApplyFuture.get());
    }

    public static void test7() throws ExecutionException, InterruptedException {
        CompletableFuture<String> orgFuture = CompletableFuture.supplyAsync(
                () -> {
                    System.out.println("当前线程名称：" + Thread.currentThread().getName());
                    throw new RuntimeException();
                }
        );

        CompletableFuture<String> exceptionFuture = orgFuture.exceptionally((e) -> {
            e.printStackTrace();
            return "出错了";
        });
        System.out.println(exceptionFuture.get());
    }

    public static void test8() throws ExecutionException, InterruptedException {
        CompletableFuture<String> orgFuture = CompletableFuture.supplyAsync(
                () -> {
                    System.out.println("当前线程名称：" + Thread.currentThread().getName());
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "test";
                }
        );

        CompletableFuture<String> rstFuture = orgFuture.whenComplete((a, throwable) -> {
            System.out.println("当前线程名称：" + Thread.currentThread().getName());
            System.out.println("上个任务执行完啦，还把" + a + "传过来");
            if ("test".equals(a)) {
                System.out.println("666");
            }
            System.out.println("233333");
        });
        System.out.println(rstFuture.get());
    }

    public static void test9() throws ExecutionException, InterruptedException {
        CompletableFuture<String> orgFuture = CompletableFuture.supplyAsync(
                () -> {
                    System.out.println("当前线程名称：" + Thread.currentThread().getName());
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "test";
                }
        );

        CompletableFuture<String> rstFuture = orgFuture.handle((a, throwable) -> {
            System.out.println("上个任务执行完啦，还把" + a + "传过来");
            if ("test".equals(a)) {
                System.out.println("666");
                return "关注了";
            }
            System.out.println("233333");
            return null;
        });
        System.out.println(rstFuture.get());
    }

}
