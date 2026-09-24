package Server.Interface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** A general request wrapper used for every TCP operation. */
public final class TCPRequest implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long m_requestId;
	private final Operation m_operation;
	private final ArrayList<Object> m_arguments;

	public TCPRequest(long requestId, Operation operation, Object... arguments)
	{
		m_requestId = requestId;
		m_operation = operation;
		m_arguments = new ArrayList<Object>(Arrays.asList(arguments));
	}

	public long getRequestId()
	{
		return m_requestId;
	}

	public Operation getOperation()
	{
		return m_operation;
	}

	public List<Object> getArguments()
	{
		return Collections.unmodifiableList(m_arguments);
	}
}
