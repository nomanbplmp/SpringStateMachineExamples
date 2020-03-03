package com.nk.exmaple.spring.statemachine;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@SpringBootApplication
public class Application implements ApplicationRunner , ApplicationContextAware{

	private static final int CONCURRENCY = 5;
	private CountDownLatch latch = new CountDownLatch(CONCURRENCY);
	public static final ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

	@Autowired
	private StateMachineFactory<String, String> machineFactory;

	private ConfigurableApplicationContext applicationContext;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		for (int i = 0; i < CONCURRENCY; i++) {
			executorService.submit(() -> this.moveHereAndTher());
		}
		latch.await();
		
		executorService.shutdownNow();
		//awaitTermination to avoid taskRejection
		cachedThreadPool.awaitTermination(1, TimeUnit.SECONDS);
		cachedThreadPool.shutdown();
		
	}

	private void moveHereAndTher() {
		StateMachine<String, String> machine = machineFactory.getStateMachine();
		machine.start();
		for (int i = 0; i < 2; i++) {
			System.out.println(Thread.currentThread().getId() + " Callig moveHereAndTher");

			Long id = Thread.currentThread().getId();
			machine.sendEvent("MOVE");
			System.out.println(Thread.currentThread().getId() + " MOVING");
			machine.sendEvent("MOVE");
		}
		latch.countDown();
		System.out.println(latch.getCount());
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext  = (ConfigurableApplicationContext) applicationContext;
		
	}

}

@Configuration
@EnableStateMachineFactory
class SpringMachineConfig extends StateMachineConfigurerAdapter<String, String> {
	
	@Override
	public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
		states.withStates().initial("HERE").state("THERE");
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
		transitions.withExternal().source("HERE").target("THERE").event("MOVE").and().withExternal().source("THERE")
				.target("HERE").event("MOVE");
	}

	@Override
	public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
		config.withConfiguration().listener(new StateMachineListenerAdapter<String, String>() {
			@Override
			public void stateChanged(State<String, String> from, State<String, String> to) {
				System.out.println(Thread.currentThread().getId() + " MOVE FROM " + from.getId() + " TO " + to.getId());
			}

			@Override
			public void eventNotAccepted(Message<String> event) {
				System.out.println("Event not access " + event.getPayload());
			}

			@Override
			public void stateMachineError(StateMachine<String, String> stateMachine, Exception exception) {
				// TODO Auto-generated method stub
				System.out.println(exception);
			}

		}).taskExecutor(new ConcurrentTaskExecutor(Application.cachedThreadPool));
	}
}
