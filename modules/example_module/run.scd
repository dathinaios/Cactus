
{
  // Use this structure to pass arguments to your modules
  // You need to use an event like so: (a: 1, b: 24)
  arg a, b;
  a ?? {a = "no argument passed for a"};
  b ?? {b = "no argument passed for b"};
  a.debug("This is arg a");
  b.debug("This is arg b");
}
