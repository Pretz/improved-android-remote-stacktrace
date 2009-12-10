<?php
        if ( $_POST['stacktrace'] == "" || $_POST['package_version'] == "" || $_POST['package_name'] == "" ) {
                die("This script is used to collect field test crash stacktraces. No personal information is transmitted, collected or stored.<br/>For more information, please contact <a href='mailto:support@nullwire.com'>email@domain.com</a>");
        }
        $random = rand(1000,9999);
        $version = $_POST['package_version'];
        $package = $_POST['package_name'];
        $handle = fopen($package."-trace-".$version."-".time()."-".$random, "w+");
        fwrite($handle, $_POST['stacktrace']);
        fclose($handle);

		// Uncomment and change the following line to have exceptions mailed to you
        //mail("mads.kristiansen@nullwire.com","IMPORTANT: Exception received (".$version.")",$_POST['stacktrace'], "from:bugs@nullwire.com");
?>
