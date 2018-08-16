package ca.cmpt276.walkinggroup.app.model;

import ca.cmpt276.walkinggroup.dataobjects.User;
import ca.cmpt276.walkinggroup.proxy.WGServerProxy;
import ca.cmpt276.walkinggroup.dataobjects.PermissionRequest;
import java.util.ArrayList;
import java.util.List;


public class Session {

    private WGServerProxy sessionProxy;
    private User sessionUser;
    private List<PermissionRequest> requests;

    private static Session session;

    private Session(){}

    public  static Session getInstance(){
        if(session == null){
            session = new Session();
        }
        return session;
    }

    public void saveSessionUser(User user){
        this.sessionUser = user;
    }

    public User getSessionUser(){
        return sessionUser;
    }

    public void saveSessionProxy(WGServerProxy saveProxy){
        this.sessionProxy = saveProxy;
    }

    public WGServerProxy getSessionProxy(){
        return sessionProxy;
    }

    public List<PermissionRequest> getRequests() {
        return requests;
    }

    public void setRequests(List<PermissionRequest> requests) {
        this.requests = requests;
    }




}
