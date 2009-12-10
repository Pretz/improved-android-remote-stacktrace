<?php
	try
	{
		//create or open the database
		$database = new SQLiteDatabase('site.db', 0666, $error);
	} catch(Exception $e) {
		die($error);
	}
?>