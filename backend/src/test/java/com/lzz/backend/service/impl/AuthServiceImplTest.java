package com.lzz.backend.service.impl;

import com.lzz.backend.common.GlobalConstant;
import com.lzz.backend.dto.AuthSessionResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceImplTest {

    @Test
    void getSessionReturnsLoggedOutWhenSessionMissing() {
        AuthServiceImpl service = new AuthServiceImpl(null);

        AuthSessionResponse response = service.getSession(null);

        assertFalse(response.isLoggedIn());
        assertNull(response.getUserId());
        assertNull(response.getUsername());
        assertNull(response.getRole());
    }

    @Test
    void getSessionReturnsLoggedInUserWhenSessionHasAttributes() {
        AuthServiceImpl service = new AuthServiceImpl(null);
        HttpSession session = new MockHttpSession();
        session.setAttribute(GlobalConstant.SESSION_USER_ID, 7L);
        session.setAttribute(GlobalConstant.SESSION_USERNAME, "alice");
        session.setAttribute(GlobalConstant.SESSION_ROLE, 0);

        AuthSessionResponse response = service.getSession(session);

        assertTrue(response.isLoggedIn());
        assertEquals(7L, response.getUserId());
        assertEquals("alice", response.getUsername());
        assertEquals(0, response.getRole());
    }

    @Test
    void logoutInvalidatesSessionWhenPresent() {
        AuthServiceImpl service = new AuthServiceImpl(null);
        MockHttpSession session = new MockHttpSession();

        service.logout(session);

        assertTrue(session.isInvalid());
    }
}
