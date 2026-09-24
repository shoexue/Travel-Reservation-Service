package Server.Interface;

import java.io.Serializable;

/** Response paired with a TCPRequest by request ID. */
public final class TCPResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	private final long m_requestId;
	private final boolean m_success;
	private final Object m_result;
	private final String m_error;

	private TCPResponse(long requestId, boolean success, Object result, String error)
	{
		m_requestId = requestId;
		m_success = success;
		m_result = result;
		m_error = error;
	}

	public static TCPResponse success(long requestId, Object result)
	{
		return new TCPResponse(requestId, true, result, null);
	}

	public static TCPResponse failure(long requestId, String error)
	{
		return new TCPResponse(requestId, false, null, error);
	}

	public long getRequestId()
	{
		return m_requestId;
	}

	public boolean isSuccess()
	{
		return m_success;
	}

	public Object getResult()
	{
		return m_result;
	}

	public String getError()
	{
		return m_error;
	}
}
