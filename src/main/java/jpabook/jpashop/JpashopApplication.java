package jpabook.jpashop;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class JpashopApplication {

	public static void main(String[] args) {
		SpringApplication.run(JpashopApplication.class, args);
	}

	// 1. jackson 라이브러리는 기본적으로 이 프록시 객체를 json으로 어떻게 생성해야 하는지 모름
	// 	-> Hibernate5Module 을 스프링 빈으로 등록하면 해결
	// 2. 지연 로딩으로 인한 null 값이 존재 (지연로딩으로 인한 것)
	// 	-> FORCE_LAZY_LOADING 옵션을 true로 키면 강제 로딩을 되게 할 수는 있음
	// 	-> 단, 해당 옵션은 order -> member , member -> orders 양방향 연관관계를 계속 로딩하게 되므로 @JsonIgnore 옵션을 한곳에 주어야 함
	@Bean
	Hibernate5Module hibernate5Module() {
		Hibernate5Module hibernate5Module = new Hibernate5Module();
//		hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING,true); // 강제 지연 로딩 설정 (사실 절대 이거 쓰면 안됨)
		return hibernate5Module;
	}
}
