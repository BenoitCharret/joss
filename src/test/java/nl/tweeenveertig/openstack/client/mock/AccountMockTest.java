package nl.tweeenveertig.openstack.client.mock;

import nl.tweeenveertig.openstack.model.Account;
import nl.tweeenveertig.openstack.model.Container;
import nl.tweeenveertig.openstack.model.PaginationMap;
import nl.tweeenveertig.openstack.model.StoredObject;
import nl.tweeenveertig.openstack.exception.CommandException;
import nl.tweeenveertig.openstack.exception.CommandExceptionError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AccountMock.class })
public class AccountMockTest {

    @Test
    public void addMetadata() {
        Account account = new AccountMock();
        account.authenticate(); // Not used in mock implementation
        Map<String, Object> metadata = new TreeMap<String, Object>();
        metadata.put("name", "Alpha");
        metadata.put("year", 1969);
        account.setMetadata(metadata);
        assertEquals("Alpha", account.getMetadata().get("name"));
        assertEquals("1969", account.getMetadata().get("year"));
    }

    @Test
    public void createContainer() {
        Account account = new AccountMock();
        Container container = account.getContainer("town1");
        container.create();
        assertTrue(container.exists());
    }

    @Test
    public void createContainerAlreadyExists() {
        Account account = new AccountMock();
        Container container = account.getContainer("town1");
        container.create();
        try {
            container.create();
            fail("Should have thrown an exception");
        } catch (CommandException err) {
            assertEquals(CommandExceptionError.ENTITY_ALREADY_EXISTS, err.getError());
        }
    }

    @Test
    public void containerDoesNotExist() {
        Account account = new AccountMock();
        try {
            account.getContainer("somevalue").delete();
            fail("Should have thrown an exception");
        } catch (CommandException err) {
            assertEquals(CommandExceptionError.ENTITY_DOES_NOT_EXIST, err.getError());
        }
    }

    @Test
    public void doubleDeleteContainer() {
        Account account = new AccountMock();
        Container container = account.getContainer("town1");
        container.create();
        container.delete();
        try {
            container.delete();
            fail("Should have thrown an exception");
        } catch (CommandException err) {
            assertEquals(CommandExceptionError.ENTITY_DOES_NOT_EXIST, err.getError());
        }
    }

    @Test
    public void deleteNonEmptyContainer() throws IOException {
        Account account = new AccountMock();
        Container container = account.getContainer("town1");
        container.create();
        StoredObject object = container.getObject("somefile");
        object.uploadObject(new byte[] { 0x01, 0x02, 0x03 });
        try {
            container.delete();
            fail("Should have thrown an exception");
        } catch (CommandException err) {
            assertEquals(CommandExceptionError.CONTAINER_NOT_EMPTY, err.getError());
        }
    }

    @Test
    public void listContainers() {
        Account account = new AccountMock();
        account.getContainer("town1").create();
        account.getContainer("town2").create();
        account.getContainer("town3").create();
        assertEquals(3, account.list().size());
    }

    @Test
    public void listContainersPaged() {
        Account account = new AccountMock();
        account.getContainer("town1").create();
        account.getContainer("town2").create();
        Container town3 = account.getContainer("town3").create();
        Container town4 = account.getContainer("town4").create();
        account.getContainer("town5").create();
        Collection<Container> towns = account.list(null, "town2", 2);
        assertEquals(2, towns.size());
        towns.contains(town3);
        towns.contains(town4);
    }

    @Test
    public void listContainersUsePaginationMap() {
        Account account = new AccountMock();
        account.getContainer("town1").create();
        account.getContainer("town2").create();
        Container town3 = account.getContainer("town3").create();
        Container town4 = account.getContainer("town4").create();
        account.getContainer("town5").create();
        PaginationMap paginationMap = account.getPaginationMap(2);
        assertEquals(3, paginationMap.getNumberOfPages());
        assertEquals(5, paginationMap.getNumberOfRecords());
        Collection<Container> towns = account.list(paginationMap, 1);
        assertEquals(2, towns.size());
        towns.contains(town3);
        towns.contains(town4);
    }

    @Test
    public void copyObject() throws IOException {
        Container container = new AccountMock().getContainer("town1");
        container.create();
        StoredObject sourceObject = container.getObject("source-object");
        StoredObject targetObject = container.getObject("target-object");
        byte[] bytes = new byte[] { 0x01, 0x02, 0x03 };
        sourceObject.uploadObject(bytes);
        sourceObject.copyObject(container, targetObject);
        assertEquals(sourceObject.downloadObject().length, targetObject.downloadObject().length);
        assertEquals(sourceObject.getContentLength(), targetObject.getContentLength());
        assertEquals(sourceObject.getEtag(), targetObject.getEtag());
    }

    @Test
    public void getInfo() throws IOException {
        Account account = new AccountMock();
        Container container1 = account.getContainer("alpha");
        container1.create();
        StoredObject object = container1.getObject("1");
        byte[] bytes = new byte[] { 0x01, 0x02, 0x03 };
        object.uploadObject(bytes);
        assertEquals(3 , account.getBytesUsed());
        assertEquals(1, account.getCount());
        assertEquals(1, account.getObjectCount());
    }

    @Test
    public void getContainer() {
        Account account = new AccountMock();
        Container container1 = account.getContainer("Alpha");
        container1.create();
        Container container2 = account.getContainer("Alpha");
        assertEquals(container1, container2);
    }

    @Test(expected = CommandException.class)
    public void setOnFileObjectStore() throws Exception {
        OnFileObjectStoreLoader loader = mock(OnFileObjectStoreLoader.class);
        whenNew(OnFileObjectStoreLoader.class).withNoArguments().thenReturn(loader);
        doThrow(new IOException()).when(loader).createContainers(any(Account.class), anyString());
        new AccountMock().setOnFileObjectStore("test");
    }

    @Test
    public void getPublicUrl() {
        assertEquals("", new AccountMock().getPublicURL());
        AccountMock account = new AccountMock()
                .setPublicUrl("http://localhost:8080/mock");
        assertEquals("http://localhost:8080/mock", account.getPublicURL());
    }

}
