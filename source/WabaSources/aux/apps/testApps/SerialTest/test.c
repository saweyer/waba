int checksum(unsigned char *ptr)
	{
int n;
int c = 0;
n = *(ptr++);
n -=2;
while(n>1) 
{
c += (*(ptr)<<8) | *(ptr+1);
printf("%d\n",c);
c = c & 0xffff;
printf("%d\n",c);
n -= 2;
ptr += 2;
}
if (n>0) c = c^(int)*(ptr++);
return(c);
}

main()
{
//-6 -5 6 90 59 130 103 59
unsigned char data[5] = {2+5,90,0x3b,13,0};
printf("%d %d\n",checksum(data) >> 8,checksum(data)&0x00ff);
}

