package com.tom.aspirated.common.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class HealthyPulse {


	@Scheduled(initialDelay = 2000, fixedRate = 1200000)
	public void printPulse() {
		log.info("Server Pulse.");
	}

}
