package com.quickblox.q_municate_core.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.core.exception.QBResponseException;
import com.quickblox.chat.errors.QBChatErrorsConstants;
import com.quickblox.q_municate_core.core.command.ServiceCommand;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.qb.helpers.QBAuthHelper;
import com.quickblox.q_municate_core.qb.helpers.QBChatRestHelper;
import com.quickblox.q_municate_core.service.QBService;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.Date;

public class QBLoginChatCommand extends ServiceCommand {

    private static final String TAG = QBLoginChatCommand.class.getSimpleName();

    private QBChatRestHelper chatRestHelper;

    public QBLoginChatCommand(Context context, QBAuthHelper authHelper, QBChatRestHelper chatRestHelper,
            String successAction, String failAction) {
        super(context, successAction, failAction);
        this.chatRestHelper = chatRestHelper;
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.LOGIN_CHAT_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        QBUser currentUser = AppSession.getSession().getUser();
        // TODO IS remove when fix ResourceBindingNotOfferedException occurrence
        tryLogin(currentUser);
        if (!chatRestHelper.isLoggedIn()) {
            throw new Exception(QBChatErrorsConstants.AUTHENTICATION_FAILED);
        }
        return extras;
    }

    private void tryLogin(QBUser currentUser) throws XMPPException, IOException, SmackException {
        long startTime = new Date().getTime();
        long currentTime = startTime;
        while (!chatRestHelper.isLoggedIn() && (currentTime - startTime) < ConstsCore.LOGIN_TIMEOUT) {
            currentTime = new Date().getTime();
            try {
                chatRestHelper.login(currentUser);
            } catch (SmackException ignore) { /* NOP */ }
        }
    }
}