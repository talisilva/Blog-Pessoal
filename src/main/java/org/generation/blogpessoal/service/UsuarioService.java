package org.generation.blogpessoal.service;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.apache.tomcat.util.codec.binary.Base64;
import org.generation.blogpessoal.model.Usuario;
import org.generation.blogpessoal.model.UsuarioLogin;
import org.generation.blogpessoal.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	public String encoder(String senha) {

		return encoder.encode(senha);

	}
	
	public Optional<Usuario> buscarUsuarioId(long id) {
		return usuarioRepository.findById(id);
		
		}
	public List<Usuario> listarUsuario() {
		return usuarioRepository.findAll();
	}

	public Optional<Usuario> cadastrarUsuario(Usuario usuario) {
		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isPresent())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O usuario ja existe!!", null);
		int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();

		if (idade < 18)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Usuário é menor de idade!", null);
		usuario.setSenha(encoder(usuario.getSenha()));

		return Optional.of(usuarioRepository.save(usuario));

	}

	public Optional<Usuario> atualizarUsuario(Usuario usuario) {
		for (Usuario user : this.listarUsuario()) {
			if (user.getUsuario().equals(usuario.getUsuario())) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Ja existe um usuario com esse nome", null);
			}
		}
		if (usuarioRepository.findByUsuario(usuario.getUsuario()).isEmpty()) {
			int idade = Period.between(usuario.getDataNascimento(), LocalDate.now()).getYears();
			if (idade < 18)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O Usuário é menor de idade!", null);

			usuario.setSenha(encoder(usuario.getSenha()));
			return Optional.of(usuarioRepository.save(usuario));

		} else {

			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "O Usuário não encontrado!", null);

		}
	}

	public Optional<UsuarioLogin> Logar(Optional<UsuarioLogin> user) {

		Optional<Usuario> usuario = usuarioRepository.findByUsuario(user.get().getUsuario());

		if (usuario.isPresent()) {
			if (encoder.matches(user.get().getSenha(), usuario.get().getSenha())) {

				String auth = user.get().getUsuario() + ":" + user.get().getSenha();
				byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(Charset.forName("US-ASCII")));
				String authHeader = "Basic " + new String(encodedAuth);

				user.get().setSenha(usuario.get().getSenha());
				user.get().setId(usuario.get().getId());
				user.get().setToken(authHeader);
				user.get().setNome(usuario.get().getNome());

				return user;
			}
		}

		return null;
	}

}