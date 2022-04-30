package io.mes.controllers;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.mes.email.Email;
import io.mes.email.EmailRepository;
import io.mes.emaillist.EmailListItem;
import io.mes.emaillist.EmailListItemKey;
import io.mes.emaillist.EmailListItemRepository;
import io.mes.folders.Folder;
import io.mes.folders.FolderRepository;
import io.mes.folders.FolderService;
import io.mes.folders.UnreadEmailStatsRepository;

@Controller
public class EmailViewController {

    @Autowired private FolderRepository folderRepository;
    @Autowired private FolderService folderService;
    @Autowired private EmailRepository emailRepository;
    @Autowired private EmailListItemRepository emailListItemRepository;
    @Autowired private UnreadEmailStatsRepository unreadEmailStatsRepository;

    
    @GetMapping(value = "/emails/{id}")
    public String emailView(
        @RequestParam String folder,
        @PathVariable UUID id,
        @AuthenticationPrincipal OAuth2User principal,
        Model model
        )
    {
        if(principal == null || !StringUtils.hasText(principal.getAttribute("login"))){
        return "index";
        }

        String userId= principal.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(userId);
        model.addAttribute("userFolders", userFolders);
        List<Folder> defaultFolders = folderService.fetchDefaulFolders(userId);
        model.addAttribute("defaultFolders", defaultFolders);
        model.addAttribute("userName", principal.getAttribute("name"));


        Optional<Email> optionalEmail = emailRepository.findById(id);
        if(!optionalEmail.isPresent()){
            return "inbox-page";
        }


        Email email = optionalEmail.get();
        
        String toIds = String.join(", ",email.getTo());
        model.addAttribute("email", email);
        model.addAttribute("toIds", toIds);

        EmailListItemKey key = new EmailListItemKey();
        key.setId(userId);
        key.setLabel(folder);
        key.setTimeUUID(email.getId());

        //Updating read status
        Optional<EmailListItem> optionalEmailListitem = emailListItemRepository.findById(key);
        if(optionalEmailListitem.isPresent()){
            EmailListItem emailListItem = optionalEmailListitem.get();
            if(emailListItem.isUnread()){
                emailListItem.setUnread(false);
                emailListItemRepository.save(emailListItem);
                unreadEmailStatsRepository.decrementUnreadCount(userId, folder);
                
            }
        }

        model.addAttribute("stats", folderService.mapCountToLabels(userId));

        return "email-page";
}
}
