package nl.tweeenveertig.openstack.client.core;

import nl.tweeenveertig.openstack.model.Account;
import nl.tweeenveertig.openstack.headers.Metadata;
import nl.tweeenveertig.openstack.headers.account.AccountMetadata;
import nl.tweeenveertig.openstack.information.AccountInformation;

public abstract class AbstractAccount extends AbstractObjectStoreEntity<AccountInformation> implements Account {

    private boolean allowReauthenticate = true;

    public AbstractAccount() {
        this.info = new AccountInformation();
    }

    public AbstractAccount setAllowReauthenticate(boolean allowReauthenticate) {
        this.allowReauthenticate = allowReauthenticate;
        return this;
    }

    public boolean isAllowReauthenticate() {
        return this.allowReauthenticate;
    }

    public int getContainerCount() {
        checkForInfo();
        return info.getContainerCount();
    }

    public long getBytesUsed() {
        checkForInfo();
        return info.getBytesUsed();
    }

    public int getObjectCount() {
        checkForInfo();
        return info.getObjectCount();
    }

    protected Metadata createMetadataEntry(String name, String value) {
        return new AccountMetadata(name, value);
    }
}
