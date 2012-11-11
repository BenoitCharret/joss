package nl.tweeenveertig.openstack.command.container;

import nl.tweeenveertig.openstack.model.Account;
import nl.tweeenveertig.openstack.command.core.AbstractSecureCommand;
import nl.tweeenveertig.openstack.command.identity.access.Access;
import nl.tweeenveertig.openstack.model.Container;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;

public abstract class AbstractContainerCommand<M extends HttpRequestBase, N extends Object> extends AbstractSecureCommand<M, N> {

    public AbstractContainerCommand(Account account, HttpClient httpClient, Access access, Container container) {
        super(account, httpClient, access.getInternalURL() + "/" + container.getName(), access.getToken());
    }
}
