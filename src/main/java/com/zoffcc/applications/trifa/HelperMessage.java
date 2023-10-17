package com.zoffcc.applications.trifa;

import com.zoffcc.applications.sorm.Message;

import java.nio.ByteBuffer;
import java.util.List;

import static com.zoffcc.applications.trifa.MainActivity.modify_message;
import static com.zoffcc.applications.trifa.MainActivity.tox_friend_get_public_key;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_MESSAGE_TYPE.TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK;

public class HelperMessage {

    private static final String TAG = "trifa.Hlp.Message";

    static void send_msgv3_high_level_ack(final long friend_number, String msgV3hash_hex_string)
    {
        if (msgV3hash_hex_string.length() < TOX_HASH_LENGTH)
        {
            return;
        }
        ByteBuffer hash_bytes = HelperGeneric.hexstring_to_bytebuffer(msgV3hash_hex_string);


        if (hash_bytes == null) {
            return;
        } else {
            long t_sec = (System.currentTimeMillis() / 1000);
            long res = MainActivity.tox_messagev3_friend_send_message(friend_number,
                    TOX_MESSAGE_TYPE_HIGH_LEVEL_ACK.value,
                    "_", hash_bytes, t_sec);
        }
    }

    public static long get_message_id_from_filetransfer_id_and_friendnum(long filetransfer_id, long friend_number)
    {
        try
        {
            List<Message> m = TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(filetransfer_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).
                    orderByIdDesc().toList();

            if (m.size() == 0)
            {
                return -1;
            }

            return m.get(0).id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:EE:" + e.getMessage());
            return -1;
        }
    }

    static void update_message_in_db_filename_fullpath_from_id(long msg_id, String filename_fullpath)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(msg_id).filename_fullpath(filename_fullpath).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath_friendnum_and_filenum(long friend_number, long file_number, String filename_fullpath)
    {
        try
        {
            long ft_id = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).orderByIdDesc().toList().get(0).id;

            update_message_in_db_filename_fullpath_from_id(TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).toList().
                    get(0).id, filename_fullpath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_state_from_id(long message_id, int state)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(message_id).state(state).execute();
            // Log.i(TAG, "set_message_state_from_id:message_id=" + message_id + " state=" + state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_state_from_friendnum_and_filenum(long friend_number, long file_number, int state)
    {
        try
        {
            long ft_id = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).orderByIdDesc().toList().get(0).id;
            // Log.i(TAG,
            //       "set_message_state_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
            //       " file_number=" + file_number);
            set_message_state_from_id(TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).
                    toList().get(0).id, state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_id(long message_id, long filedb_id)
    {
        try
        {
            TrifaToxService.Companion.getOrma().updateMessage().idEq(message_id).filedb_id(filedb_id).execute();
            // Log.i(TAG, "set_message_filedb_from_id:message_id=" + message_id + " filedb_id=" + filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_friendnum_and_filenum(long friend_number, long file_number, long filedb_id)
    {
        try
        {
            long ft_id = TrifaToxService.Companion.getOrma().selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key(friend_number)).
                    file_numberEq(file_number).
                    orderByIdDesc().toList().
                    get(0).id;
            // Log.i(TAG,
            //       "set_message_filedb_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number +
            //       " file_number=" + file_number);
            set_message_filedb_from_id(TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(ft_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key(friend_number)).
                    orderByIdDesc().toList().
                    get(0).id, filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void update_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    if (message_id != -1)
                    {
                        try
                        {
                            Message m = TrifaToxService.Companion.getOrma().selectFromMessage().idEq(message_id).orderByIdDesc().toList().get(0);

                            if (m.id != -1)
                            {
                                // if (force)
                                {
                                    modify_message(m);
                                }
                            }
                        }
                        catch (Exception e2)
                        {
                        }
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static void update_single_message_from_ftid(final long filetransfer_id, final boolean force)
    {
        try
        {
            Message m = TrifaToxService.Companion.getOrma().selectFromMessage().
                    filetransfer_idEq(filetransfer_id).
                    orderByIdDesc().toList().get(0);

            if (m.id != -1)
            {
                // if (force)
                {
                    modify_message(m);
                }
            }
        }
        catch (Exception e2)
        {
            e2.printStackTrace();
        }
    }

}
