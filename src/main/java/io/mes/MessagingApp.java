package io.mes;
import java.nio.file.Path;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.internal.core.type.codec.UuidCodec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mes.email.Email;
import io.mes.email.EmailRepository;
import io.mes.email.EmailService;
import io.mes.emaillist.EmailListItem;
import io.mes.emaillist.EmailListItemKey;
import io.mes.emaillist.EmailListItemRepository;
import io.mes.folders.Folder;
import io.mes.folders.FolderRepository;
import io.mes.folders.UnreadEmailStatsRepository;

@SpringBootApplication
@RestController
public class MessagingApp {

	@Autowired FolderRepository folderRepository;
	@Autowired EmailRepository emailRepository;
	@Autowired EmailService emailService;


	public static void main(String[] args) {
		SpringApplication.run(MessagingApp.class, args);
	}

	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties){
		Path bundle = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(bundle);
	}
	
	@PostConstruct
	public void init(){
		
		folderRepository.save(new Folder("Abhi-k-s","Work","blue"));
		folderRepository.save(new Folder("Abhi-k-s","Home","green"));
		folderRepository.save(new Folder("Abhi-k-s","Important","yellow"));


		for (int i = 0; i < 10; i++){
			emailService.sendEmail("Abhi-k-s", Arrays.asList("Abhi-k-s","ooadj"), "Hello" + i, "Body");
			
		}
	}
}
