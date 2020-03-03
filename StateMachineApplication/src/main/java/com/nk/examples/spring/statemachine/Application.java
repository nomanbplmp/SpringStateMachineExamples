package com.nk.examples.spring.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@SpringBootApplication
public class Application implements CommandLineRunner {

	@Autowired
	private StateMachine<String, String> machine;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		machine.sendEvent("MOVE");
		machine.sendEvent("MOVE");
		
	}

}

@Configuration
@EnableStateMachine
class StateMachineConfig extends StateMachineConfigurerAdapter<String, String> {

	private static final String MOVE = "MOVE";
	private static final String THERE = "THERE";
	private static final String HERE = "HERE";

	@Override
	public void configure(StateMachineConfigurationConfigurer<String, String> config) throws Exception {
		// TODO Auto-generated method stub
		config.withConfiguration().autoStartup(true).listener(new StateMachineListenerAdapter<String, String>() {
			@Override
			public void stateChanged(State<String, String> from, State<String, String> to) {
				System.out.println("Moved FROM " + from.getId() + " to " + to.getId());
			}
		});
	}

	@Override
	public void configure(StateMachineStateConfigurer<String, String> states) throws Exception {
		states.withStates().initial(HERE).state(THERE);
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<String, String> transitions) throws Exception {
		transitions.withExternal().source(HERE).target(THERE).event(MOVE).and().withExternal().source(THERE)
				.target(HERE).event(MOVE);
	}

}
