package br.com.cdsoft.transaction.transaction.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TemplateSlack {


    public TemplateSlack() {

    }


    private String text;
    private List<SlackMessage> attachments = new ArrayList<>();


}
