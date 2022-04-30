package io.mes.controllers;

import java.sql.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.mes.emaillist.EmailListItem;
import io.mes.emaillist.EmailListItemRepository;
import io.mes.folders.Folder;
import io.mes.folders.FolderRepository;
import io.mes.folders.FolderService;
import io.mes.folders.UnreadEmailStats;
import io.mes.folders.UnreadEmailStatsRepository;

@Controller
public class inboxController {

    @Autowired private FolderRepository folderRepository;
    @Autowired private FolderService folderService;
    @Autowired private EmailListItemRepository emailListItemRepository;

    @GetMapping(value = "/")
    public String homePage(
        @RequestParam(required = false) String folder,
        @AuthenticationPrincipal OAuth2User principal,
        Model model
        )
    {
        if(principal == null || !StringUtils.hasText(principal.getAttribute("login"))){
        return "index";
        }

        //fetching folders
        String userId= principal.getAttribute("login");
        List<Folder> userFolders = folderRepository.findAllById(userId);
        model.addAttribute("userFolders", userFolders);
        List<Folder> defaultFolders = folderService.fetchDefaulFolders(userId);
        model.addAttribute("defaultFolders", defaultFolders);
        model.addAttribute("stats", folderService.mapCountToLabels(userId));
        model.addAttribute("userName", principal.getAttribute("name"));

        //fetching messages
        if(!StringUtils.hasText(folder)){
            folder = "Inbox";
        }
       List<EmailListItem> emailList = emailListItemRepository.findAllByKey_IdAndKey_label(userId, folder);
       PrettyTime p = new PrettyTime();
        emailList.stream().forEach(emailItem -> {
            UUID timeUuid = emailItem.getKey().getTimeUUID();
            Date emailDateTime = new Date(Uuids.unixTimestamp(timeUuid));
            emailItem.setAgoTimeString(p.format(emailDateTime));
        });
        model.addAttribute("emailList", emailList);
        model.addAttribute("folderName", folder);

        return "inbox-page";
    }
}
