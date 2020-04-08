// any file put in here gets loaded ->
a.at[\definitions] = ( Document.current.dir ++ "/definitions/*").pathMatch.collect{|file| file.load };
