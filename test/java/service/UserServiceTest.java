package service;

import org.example.dao.UserDAOImpl;
import org.example.model.User;
import org.example.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {
    private UserDAOImpl userDAOMock;
    private UserServiceImpl userService;

    @BeforeEach
        void setup(){
        userDAOMock = Mockito.mock(UserDAOImpl.class);
        userService = new UserServiceImpl(userDAOMock);
    }

    @Test
    void createUser_callsDAO() throws Exception {
        User user = new User("joão", 1);

        userService.createUser(user);

        verify(userDAOMock, times(1)).create(user);
    }

    @Test
    void getUserById_returnsUser() throws Exception {
        User expected = new User("Maria", 5);
        when(userDAOMock.getById(5)).thenReturn(expected);

        User result = userService.getUserById(5);
        assertEquals("Maria", result.getNome());
        assertEquals(5, result.getId());
    }

    @Test
    void getUserById_notFound_throwsException() throws Exception {
        when(userDAOMock.getById(99)).thenReturn(null);

        Exception ex = assertThrows(RuntimeException.class, () -> userService.getUserById(99));
        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
    }
}
