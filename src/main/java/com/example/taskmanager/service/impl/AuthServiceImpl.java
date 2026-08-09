package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.LoginRequestDTO;
import com.example.taskmanager.dto.LoginResponseDTO;
import com.example.taskmanager.dto.RegistroRequestDTO;
import com.example.taskmanager.dto.UsuarioResponseDTO;
import com.example.taskmanager.entity.Usuario;
import com.example.taskmanager.exception.CredenciaisInvalidasException;
import com.example.taskmanager.exception.RegraDeNegocioException;
import com.example.taskmanager.mapper.UsuarioMapper;
import com.example.taskmanager.repository.UsuarioRepository;
import com.example.taskmanager.security.JwtTokenProvider;
import com.example.taskmanager.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioMapper usuarioMapper;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            UsuarioMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.usuarioMapper = usuarioMapper;
    }

    @Override
    public UsuarioResponseDTO registrar(RegistroRequestDTO registroRequestDTO) {
         usuarioRepository.findByUsername(registroRequestDTO.username())
                 .ifPresent(usuario -> {
                             throw new RegraDeNegocioException(String.format("%s está em uso ", usuario.getUsername()));
                         });
        Usuario usuario = usuarioMapper.toEntity(registroRequestDTO);
        usuario.setSenha(passwordEncoder.encode(registroRequestDTO.senha()));

        Usuario usuarioSalvo = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(usuarioSalvo);
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {

        Usuario usuario = usuarioRepository.findByUsername(loginRequestDTO.username())
                .orElseThrow(() -> new CredenciaisInvalidasException("Credenciais inválidas"));

        if(!passwordEncoder.matches(loginRequestDTO.senha(), usuario.getSenha())) {
            throw new CredenciaisInvalidasException("Credenciais inválidas");
        }
        String token = jwtTokenProvider.gerarToken(usuario.getUsername());
        return new LoginResponseDTO(token, usuario.getNome());
    }
}
