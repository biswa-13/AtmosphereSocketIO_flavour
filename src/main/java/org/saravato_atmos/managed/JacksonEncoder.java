package org.saravato_atmos.managed;

import java.io.IOException;

import javax.inject.Inject;

import org.atmosphere.config.managed.Encoder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonEncoder implements Encoder<Message, String>{

	@Inject
	private ObjectMapper mapper;
	@Override
	public String encode(Message m) {
		try {
			return mapper.writeValueAsString(m);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

}
