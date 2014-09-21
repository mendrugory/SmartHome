package com.example.mendrugory.beans;

/**
 * Created by mendrugory on 20/09/14.
 */
public class Response
{

    private Boolean status;
    private Integer code;
    private String message;


    public Boolean isStatus()
    {
        return status;
    }

    public void setStatus(Boolean status)
    {
        this.status = status;
    }

    public Integer getCode()
    {
        return code;
    }

    public void setCode(Integer code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }


}
