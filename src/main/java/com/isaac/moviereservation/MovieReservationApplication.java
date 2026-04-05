package com.isaac.moviereservation;
 
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
 
@SpringBootApplication
@EnableScheduling   // habilita o @Scheduled no ReservationService
public class MovieReservationApplication {
 
    public static void main(String[] args) {
        SpringApplication.run(MovieReservationApplication.class, args);
    }
}